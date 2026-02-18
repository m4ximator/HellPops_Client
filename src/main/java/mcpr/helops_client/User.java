package mcpr.helops_client;
import commun.Jeton;
import java.util.Scanner;

public class User {
	
	private String username;
	private String password;
	private Jeton jeton;
	private Scanner Scanner;
	
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public Jeton getJeton() {
		return jeton;
	}
	public void setJeton(Jeton jeton) {
		this.jeton = jeton;
	}
	
	
	
	

}
