package com.univer.universerver.source.service;

import java.time.LocalDateTime;
import java.util.*;

import com.univer.universerver.source.model.*;
import com.univer.universerver.source.model.request.UserRequest;
import com.univer.universerver.source.model.request.admin.AdminUserRequest;
import com.univer.universerver.source.repository.*;
import com.univer.universerver.source.security.jwt.JwtAuthTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.univer.universerver.source.common.response.ErrorCode;
import com.univer.universerver.source.common.response.exception.UserException;
import com.univer.universerver.source.model.request.SignUpForm;
import com.univer.universerver.source.utils.RoleName;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UniversityRepository universityRepository;
    @Autowired
    private UserImageRepository userImageRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CommonRepository commonRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);


    public void registerUser (SignUpForm signUpForm)
    {
    	
		if(userRepository.existsByUserid(signUpForm.getUserid())) {
			throw new UserException(ErrorCode.USERID_DUPLICATION);
	    }
        if(userRepository.existsByNickname(signUpForm.getNickname())) {
        	throw new UserException(ErrorCode.NICKNAME_DUPLICATION);
        }
        User user = new User(signUpForm, encoder.encode(signUpForm.getPassword()));

        Set<String> strRoles = new HashSet<>();
        strRoles.add("user");
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            switch(role) {
                case "admin":
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                    roles.add(adminRole);
                    break;
                default:
                    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
                    roles.add(userRole);
            }
        });

//        for(int i=0;i<signUpForm.getUserImages().size();i++){
//            System.out.println(signUpForm.getUserImages().get(i));
//        }
        User rtnUser = userRepository.save(user);
        for(int i=0;i<signUpForm.getUserImages().length;i++){
            UserImage userImage = new UserImage();
            userImage.setProfileImg(signUpForm.getUserImages()[i]);
            userImage.setUser(rtnUser);
            userImageRepository.save(userImage);
        }
        user.setRoles(roles);

    }

    public Optional<User> findUserId(long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserLoginId(String userid) {
        return userRepository.findByUserid(userid);
    }

    //마지막 로그인 시간 저장
    public void saveAccessTime(User user) {
        LocalDateTime dateTime= LocalDateTime.now();
        user.setLastaccesstime(dateTime);
        userRepository.save(user);
    }

    public Optional<User> findMyUserInfo(String userId) {
        return userRepository.findByUserid(userId);
    }

    public void myInfoDelete(String userName) {
        Optional<User> user = userService.findMyUserInfo(userName);
        user.get().setDeleteYn("Y");
        userRepository.save(user.get());
    }

    public long myInfoNotiUpdate(String userName, String noti) {
        Optional<User> user = userService.findMyUserInfo(userName);
        if(noti.equals("0")){
            user.get().setNotiYn("N");
        }else{
            user.get().setNotiYn("Y");
        }
        return Integer.parseInt(noti);
    }


    public void createFcmToken(String userName, String fcmToken) {
        Optional<User> user = userService.findMyUserInfo(userName);
        user.get().setFcmToken(fcmToken);
        if(user.get().getFcmToken().equals(fcmToken)){
            return;
        }
        userRepository.save(user.get());
    }

    public Optional<User> selectNickname(String nickName) {
        return userRepository.findByNickname(nickName);
    }

    public Optional<User> checkUserId(String userId) {
        return userRepository.findByUserid(userId);
    }

    public void updateUser(UserRequest userRequest) {
        userImageRepository.deleteByUserId(userRequest.getId());
        Optional<User> user = userRepository.findById(userRequest.getId());

        Arrays.stream(userRequest.getUserImages()).forEach(imageUrl-> {

            UserImage userImage = new UserImage();
            userImage.setUser(user.get());
            userImage.setProfileImg(imageUrl);
            userImageRepository.save(userImage);
        });
        user.ifPresent(item->{

            user.get().setNickname(userRequest.getNickname());
            user.get().setIntroduce(userRequest.getIntroduce());
            userRepository.save(user.get());
        });
    }

    public List<User> findAllUsr() {
        return userRepository.findAll();
    }

    public void validUser(AdminUserRequest adminUserRequest) {
        Optional<User> user=userRepository.findById(adminUserRequest.getId());
        user.ifPresent(userOne -> {
            userOne.setVerified(true);
            userRepository.save(userOne);
        });
    }

    public List<University> findAllUniversity() {
        return universityRepository.findAll();
    }

    public List<University> findUniversity(String name) {
        return universityRepository.findByNameLike(name+"%");
    }

    public List<Common> findInterested() {
        return commonRepository.findByComCd("CD01");
    }
}
