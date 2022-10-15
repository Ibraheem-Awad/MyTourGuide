package com.example.mytourguide;

class Guide {

    public String Username,fullName,E_mail,password,phone,DOB,enabled;
    public Guide(){
    }

    public Guide(String Username,String fullName,String E_mail,String pass,String phone,String DOB,String enabled){
        this.Username = Username;
        this.fullName = fullName;
        this.E_mail = E_mail;
        this.password = pass;
        this.phone = phone;
        this.DOB = DOB;
        this.enabled = enabled;
    }
}
