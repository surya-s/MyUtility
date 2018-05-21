package com.tmk.app;

import com.tmk.model.User;
import com.tmk.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")

public class UserController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        logger.info("Getting all users.");
        return userRepository.findAll();
    }

    @GetMapping(value = "/{userId}")
    public Optional<User> getUser(@PathVariable String userId) {
        logger.info("Getting user with ID: {}.", userId);
        return userRepository.findById(userId);
    }

    @PostMapping(value = "/create")
    public User addNewUsers(@RequestBody User user) {
        logger.info("Saving user.");
        return userRepository.save(user);
    }

    @GetMapping(value = "/settings/{userId}")
    public Object getAllUserSettings(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getUserSettings();
        } else {
            return "User not found.";
        }
    }

    @RequestMapping(value = "/settings/{userId}/{key}", method = RequestMethod.GET)
    public String getUserSetting(@PathVariable String userId, @PathVariable String key) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getUserSettings().get(key);
        } else {
            return "User not found.";
        }
    }

    @RequestMapping(value = "/settings/{userId}/{key}/{value}", method = RequestMethod.GET)
    public String addUserSetting(@PathVariable String userId, @PathVariable String key, @PathVariable String value) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().getUserSettings().put(key, value);
            userRepository.save(user.get());
            return "Key added";
        } else {
            return "User not found.";
        }
    }


}
