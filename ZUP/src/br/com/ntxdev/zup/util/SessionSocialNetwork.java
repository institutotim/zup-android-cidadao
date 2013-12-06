package br.com.ntxdev.zup.util;

import br.com.ntxdex.zup.twitter.TwitterApp;

import com.facebook.Session;

public class SessionSocialNetwork {

	//Pega instância da classe
	private static SessionSocialNetwork instance;
	
	private String nome = null;
	private String usuario = null;
	private String imageProfile = null;
	private String email = null;
	private String linkProfile = null;
	
	private Session sessionFacebook = null;
	TwitterApp sessionTwitter = null;

	private SessionSocialNetwork() {
		
	}

	public static SessionSocialNetwork getInstance() {
		if (instance == null) {
			instance = new SessionSocialNetwork();
		}
		return instance;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getImageProfile() {
		return imageProfile;
	}

	public void setImageProfile(String imageProfile) {
		this.imageProfile = imageProfile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLinkProfile() {
		return linkProfile;
	}

	public void setLinkProfile(String linkProfile) {
		this.linkProfile = linkProfile;
	}

	public Session getSessionFacebook() {
		return sessionFacebook;
	}

	public void setSessionFacebook(Session sessionFacebook) {
		this.sessionFacebook = sessionFacebook;
	}

	public TwitterApp getSessionTwitter() {
		return sessionTwitter;
	}

	public void setSessionTwitter(TwitterApp sessionTwitter) {
		this.sessionTwitter = sessionTwitter;
	}

	public static void setInstance(SessionSocialNetwork instance) {
		SessionSocialNetwork.instance = instance;
	}
	
	
}
