package com.upm.androidnewsletter.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.Hashtable;

import com.upm.androidnewsletter.model.json.JSONObject;

public class Image extends ModelEntity{
	private int order;
	private String description;
	private int idArticle;
	private String image;
	
	/**
	 * Consructor of an Image, always through article, because an image shouldn't exist alone without one article
	 * @param mm ModelManager used to communicate with remote service 
	 * @param order of the image within the article
	 * @param description of the image
	 * @param idArticle - id of article of the image
	 * @param b64Image - data of the image
	 */
	protected Image(ModelManager mm,int order, String description, int idArticle, String b64Image){
		super(mm);
		this.id=-1;
		this.order = order;
		this.description = description;
		this.idArticle=idArticle;
		this.image = Utils.createScaledStrImage(b64Image,500,500);
	}
	
	/**
	 * 
	 * @param mm
	 * @param jsonImage
	 */
	@SuppressWarnings("unchecked")
    public Image(ModelManager mm, JSONObject jsonImage){
		super(mm);
		try{
			id = Integer.parseInt(jsonImage.get("id").toString());
			order = Integer.parseInt(jsonImage.get("order").toString());
			description = jsonImage.getOrDefault("description","").toString().replaceAll("\\\\","");
			idArticle = Integer.parseInt(jsonImage.get("id_article").toString().replaceAll("\\\\",""));
			image = (jsonImage.get("data").toString().replaceAll("\\\\",""));
		}catch(Exception e){
			Logger.log(Logger.ERROR, "ERROR: Error parsing Image: from json"+jsonImage+"\n"+e.getMessage());
			throw new IllegalArgumentException("ERROR: Error parsing Image: from json"+jsonImage);
		}
	}	
	
	/**
	 * 
	 * @return
	 */
	public int getOrder() {
		return order;
	}
	/**
	 * 
	 * @param order
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	/**
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * 
	 * @return
	 */
	public int getIdArticle() {
		return idArticle;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return "Image [id=" + getId() + ", order=" + order + 
				", description=" + description + 
				", id_article=" + idArticle + 
				", data=" + image + "]";
	}
	
	protected Hashtable<String,String> getAttributes(){
		Hashtable<String,String> res = new Hashtable<String,String>();
		res.put("id_article", ""+idArticle);
		res.put("order", ""+order);
		res.put("description", description);
		res.put("data", image);
		res.put("media_type", "image/png");
		
		return res;
	}
	
	public String getImage(){
		return image;
	}

	public Bitmap getBitmapImage() {
		// Decode the Base64 string to a byte array
		byte[] decodedString = Base64.decode(this.getImage(), Base64.DEFAULT);

		// Convert the byte array to a Bitmap
		return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
	}
}
