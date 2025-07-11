package org.kosa.shoppingmaillmanager.user;

public class LoginUserDTO {
    private String user_id;
    private String name;
    private String grade_id;

    public LoginUserDTO(String user_id, String name, String email, String grade_id) {
        this.user_id = user_id;
        this.name = name;
        this.grade_id = grade_id;
    }

    public LoginUserDTO(User user) {
        this.user_id = user.getUser_id();
        this.name = user.getName();
        this.grade_id = user.getGrade_id();
    }

    // getter만 있으면 됨 (setter 없어도 됨)
    public String getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }
    
    public String getGrade_id() {
    	return grade_id;
    }
}