package br.com.ntxdev.zup.rest;

public class LoginResponseVO {

	private UserVO user;
	private String token;

	public UserVO getUser() {
		return user;
	}

	public void setUser(UserVO user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
