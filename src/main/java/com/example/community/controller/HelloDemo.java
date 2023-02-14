package com.example.community.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author yao 2022/4/12
 */
@Api(tags = "测试用接口")
@Deprecated
@RestController
public class HelloDemo {
    @ApiOperation("Hello World")
    @GetMapping("/hello")
    public ArrayList<Student> sayHello() {
        Student stu1 = new Student(1,"张三",23);
        Student stu2 = new Student(2,"李四",22);
        Student stu3 = new Student(3,"王五",32);
        ArrayList<Student> students = new ArrayList<>();
        students.add(stu1);
        students.add(stu2);
        students.add(stu3);
        return students;
    }
}

class Student implements Serializable {
    int id;
    String name;
    int age;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Student(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}
