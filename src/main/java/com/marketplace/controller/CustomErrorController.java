package com.marketplace.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        ModelAndView modelAndView = new ModelAndView("error");
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                modelAndView.addObject("errorCode", "403");
                modelAndView.addObject("errorMessage", "Access Denied");
                modelAndView.addObject("errorDescription", "You don't have permission to access this page.");
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                modelAndView.addObject("errorCode", "404");
                modelAndView.addObject("errorMessage", "Page Not Found");
                modelAndView.addObject("errorDescription", "The page you are looking for doesn't exist.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                modelAndView.addObject("errorCode", "500");
                modelAndView.addObject("errorMessage", "Internal Server Error");
                modelAndView.addObject("errorDescription", "Something went wrong on our end. Please try again later.");
            } else {
                modelAndView.addObject("errorCode", statusCode);
                modelAndView.addObject("errorMessage", "Error");
                modelAndView.addObject("errorDescription", "An unexpected error occurred.");
            }
        }
        
        return modelAndView;
    }
} 