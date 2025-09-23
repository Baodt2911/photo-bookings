package vn.baodt2911.photobooking.photobooking.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/v1/api")
public class BaseController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello " ;
    }
    
}
