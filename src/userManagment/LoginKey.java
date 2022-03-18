package userManagment;

public class LoginKey {
	
	public User user;
	public String key;
	private long keyExpire;
	private int duration;
	
	public LoginKey(User user, String key, float duration) {
		this.user = user;
		this.key = key;
		this.duration = (int)(duration * 1000 * 60);
		resetExpire();
	}
	
	public boolean checkExpire() {
		if(keyExpire < System.currentTimeMillis()) {
			return false;
		} else {
			resetExpire();
			return true;
		}
	}
	
	public void resetExpire() {
		keyExpire = System.currentTimeMillis() + duration;
	}
	
	@Override
	public String toString() {
		return "LoginKey:{user=\"" + user.getName() + "\", key=\"" + key + "\", expire=" + keyExpire + ", duration=" + duration + "}";
	}
}
