class User {
    private String username;
    private String password;
    private Role role;
    
    public User(String username, String password, Role role)
    {
        this.username = username;
        this.password = password;
        this.role = role;
    }

public String getUsername(){
        return this.username;
    }
public boolean checkPassword(String password){
        return this.password.equals(password);
    }

public Role getRole(){
        return this.role;
    }
}