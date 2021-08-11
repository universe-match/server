package com.univer.universerver.source.security.service;



import com.univer.universerver.source.model.User;
import com.univer.universerver.source.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUserid(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with -> username or email : " + username)
                );

        return UserPrinciple.build(user);
    }
    @Transactional
    public UserDetails loadUserByUserId(String username) throws UsernameNotFoundException{
        User user = userRepository.findByUserid(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with -> userNickname : " + username));
        return UserPrinciple.build(user);
    }
}
