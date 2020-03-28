package com.xiaobai.service.crud;


import com.xiaobai.pojo.Users;

public interface UserCRUDService {
	
	
	public void saveUser(Users user);
	
	
	public void updateUser(Users user);
	
	public void updateUserExample(Users user);
	
	public void delete();
}
