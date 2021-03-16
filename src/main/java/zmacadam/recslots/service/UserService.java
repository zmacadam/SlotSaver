package zmacadam.recslots.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import zmacadam.recslots.model.Role;
import zmacadam.recslots.model.User;
import zmacadam.recslots.repository.RoleRepository;
import zmacadam.recslots.repository.UserRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User findUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    public User findUserByUserName(String userName) { return userRepository.findByUserName(userName); }
    public List<User> findByActiveTrue() { return userRepository.findByActiveTrue(); }
    public List<User> findByPaidTrue() { return userRepository.findByPaidTrue(); }

    public User saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByRole("USER");
        user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void removeUser(User user) {
        userRepository.delete(user);
        return;
    }

    public User activateUser(User user) {
        user.setActive(true);
        return userRepository.save(user);
    }

}
