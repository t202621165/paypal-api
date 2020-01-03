package com.iwanol.paypal.payee.daifu;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.iwanol.paypal.domain.Payee;
import com.iwanol.paypal.vo.PayeeVo;
import com.iwanol.paypal.vo.Result;

@Component
public abstract class DaiFu_Payee {

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** 发起付款 */
	public abstract JSONObject pay(Payee payee, PayeeVo v); // 子类实现
	
	public abstract String mark();
	
	protected String type(){
		return mark();
	}

	public final JSONObject init(Payee payee, PayeeVo v) {
		try {
			if (v.getType().equals("tx") || v.getType().equals("payee")){
				JSONObject deduct = deduct(v);
				if (deduct.getBooleanValue("state")) {
					JSONObject insertSettle = new JSONObject();
					if (v.getType().equals("tx"))
						insertSettle = this.insertSettle(v,-6);					
					if (v.getType().equals("payee"))
						insertSettle = this.insertSettle(v,3);	
					if (insertSettle.getBooleanValue("state") && v.getType().equals("payee")){
						JSONObject pay = pay(payee, v);
						if (pay.getBooleanValue("state")) {
							return pay;
						} 
					}else if (insertSettle.getBooleanValue("state")){
						return Result.success.toJson("代付提交成功");
					}else{
						return Result.error.toJson("代付提交失败",-3);
					}
					JSONObject rollback = rollback(v);
					logger.info("代付金额：{},手续费：{},已回退到商户：{}账户内", v.getTotal_amount(), v.getCost(), v.getMerchant_id());
					return rollback;
				}
				return deduct;
			}else if (v.getType().equals("txPayee")){
				JSONObject pay = pay(payee, v);
				if (pay.getBooleanValue("state")) {
					logger.info("更新代付状态为处理中");
					Long mId = Long.valueOf(v.getmId());
					String sql = "update settle_ment set discription = ?,state = 3 where serial_number = ? and merchant_id = ?";
					jdbcTemplate.update(sql,new PreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps) throws SQLException {
							// TODO Auto-generated method stub
							ps.setString(1, v.getDiscription());
							ps.setString(2, v.getMerchant_order());
							ps.setLong(3, mId);
						}
					});
					return pay;
				}
				JSONObject rollback = rollback(v);
				logger.info("代付金额：{},手续费：{},已回退到商户：{}账户内", v.getTotal_amount(), v.getCost(), v.getMerchant_id());
				return rollback;
			}else{
				return Result.error.toJson("未知业务",-3);
			}
		} catch (Exception e) {
			// TODO: handle exception
			JSONObject rollback = rollback(v);
			logger.info("代付金额：{},手续费：{},已回退到商户：{}账户内", v.getTotal_amount(), v.getCost(), v.getMerchant_id());
			return rollback;
		}finally {
			
		}
	}
	

	/**
	 * 扣款
	 * 
	 * @param v
	 * @return
	 */
	public JSONObject deduct(PayeeVo v) {
		String sql = "update bank set over_money = over_money - ?,all_pay = all_pay + ? where merchant_id = ? and bank_type = 1";
		int i = jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				// TODO Auto-generated method stub
				BigDecimal amount = new BigDecimal(v.getTotal_amount()).add(new BigDecimal(v.getCost()));
				ps.setBigDecimal(1, amount);
				ps.setBigDecimal(2, amount);
				ps.setLong(3, Long.valueOf(v.getmId()));
			}
		});
		if (i > 0) {
			return Result.success.toJson("余额扣除成功");
		}
		return Result.error.toJson("余额扣除失败",-3);
	}

	/**
	 * 回退
	 * 
	 * @param v
	 * @return
	 */
	public JSONObject rollback(PayeeVo v) {
		String sql = "update bank set over_money = over_money + ?,all_pay = all_pay - ? where merchant_id = ? and bank_type = 1";
		int i = jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				// TODO Auto-generated method stub
				BigDecimal amount = new BigDecimal(v.getTotal_amount()).add(new BigDecimal(v.getCost()));
				ps.setBigDecimal(1, amount);
				ps.setBigDecimal(2, amount);
				ps.setLong(3, Long.valueOf(v.getmId()));
			}
		});
		if (i > 0) {
			logger.info("代付失败,余额回退成功");
			String discription = "代付失败(代付金额已回退)";
			Long mId = Long.valueOf(v.getmId());
			sql = "update settle_ment set discription = ?,state = -3 where serial_number = ? and merchant_id = ?";
			jdbcTemplate.update(sql,new PreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					// TODO Auto-generated method stub
					ps.setString(1, discription);
					ps.setString(2, v.getMerchant_order());
					ps.setLong(3, mId);
				}
			});
			return Result.error.toJson("代付失败",-3);
		}
		logger.info("代付失败,余额回退失败");
		return Result.error.toJson("代付失败",-3);
	}

	public JSONObject insertSettle(PayeeVo v,Integer state) {
		String sql = "insert into settle_ment(amount,apply_date,cost,discription,serial_number,state,merchant_id,bank_mark,bank_name,bank_number,real_name,reply_state,type) values(?,?,?,?,?,?,?,?,?,?,?,0,0)";
		int i = jdbcTemplate.update(sql, new PreparedStatementSetter() {	
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				// TODO Auto-generated method stub
				ps.setBigDecimal(1, new BigDecimal(v.getTotal_amount()));
				ps.setTimestamp(2,new Timestamp(new Date().getTime()));
				ps.setBigDecimal(3, new BigDecimal(v.getCost()));
				ps.setString(4, v.getDiscription());
				ps.setString(5, v.getMerchant_order());
				ps.setInt(6, state);
				ps.setLong(7, Long.valueOf(v.getmId()));
				ps.setString(8, v.getBank_code());
				ps.setString(9, v.getBank_name());
				ps.setString(10, v.getBank_number());
				ps.setString(11, v.getReal_name());
			}
		});
		if (i > 0){
			return Result.success.toJson("插入代付记录成功");
		}
		return Result.error.toJson("插入代付记录失败",-3);
	}
	
}
