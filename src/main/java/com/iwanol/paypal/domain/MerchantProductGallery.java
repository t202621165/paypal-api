package com.iwanol.paypal.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * 商户产品通道实体
 * @author leo
 *
 */
@Entity
public class MerchantProductGallery {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id; //主键id
	
	@ManyToOne
	private Product product;
	
	@ManyToOne
	private Gallery gallery;
	
	@ManyToOne
	private Merchant merchant;
	
	@Transient
	private Long pId; //产品Id
	
	@Transient
	private Long gId; //通道Id 0关闭商户产品通道 
	
	@Transient
	private Long mId; 
	
	@Transient
	private Boolean isBank;
	
	public MerchantProductGallery() {
		super();
	}
	
	public MerchantProductGallery(Merchant merchant, Product product) {
		this.merchant = merchant;
		this.product = product;
	}
	
	public List<MerchantProductGallery> listByProduct(List<Merchant> merchants, Product product) {
		List<MerchantProductGallery> list = new ArrayList<MerchantProductGallery>();
		merchants.stream().forEach(merchant -> {
			list.add(new MerchantProductGallery(merchant, product));
		});
		return list;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Gallery getGallery() {
		return gallery;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	
	public Long getpId() {
		return pId;
	}

	public void setpId(Long pId) {
		this.pId = pId;
	}

	public Long getgId() {
		return gId;
	}

	public void setgId(Long gId) {
		this.gId = gId;
	}

	public Long getmId() {
		return mId;
	}

	public void setmId(Long mId) {
		this.mId = mId;
	}

	public Boolean getIsBank() {
		return isBank;
	}

	public void setIsBank(Boolean isBank) {
		this.isBank = isBank;
	}
	
	

	@Override
	public String toString() {
		return "MerchantProductGallery [id=" + id + ", productId=" + pId + ", galleryId=" + gId
				+ ", merchantId=" + mId + ", isBank=" + isBank + "]";
	}

}
