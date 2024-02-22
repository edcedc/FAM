package com.spit.fam.Response;


import com.spit.fam.Entity.User;

public class LoginResponse {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


/*{
    "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwiaWF0IjoxNTk5MDEwODc2LCJleHAiOjE2MDE2MDI4NzZ9.HvuK6WVbKdmQn93v3n0vosM7tX1NbvgRKLXWMrhzsAI",
    "user": {
        "id": 1,
        "username": "chris",
        "email": "chrisyu2020@protonmail.com",
        "provider": "local",
        "confirmed": false,
        "blocked": false,
        "role": {
            "id": 1,
            "name": "Authenticated",
            "description": "Default role given to authenticated user.",
            "type": "authenticated",
            "created_by": null,
            "updated_by": null
        },
        "created_by": {
            "id": 1,
            "firstname": "Chris",
            "lastname": "YU",
            "username": null
        },
        "updated_by": {
            "id": 1,
            "firstname": "Chris",
            "lastname": "YU",
            "username": null
        },
        "created_at": "2020-09-01T07:08:58.000Z",
        "updated_at": "2020-09-01T09:04:02.000Z",
        "user_group": {
            "id": 1,
            "Name": "OrdinaryUser",
            "created_by": 1,
            "updated_by": 1,
            "created_at": "2020-09-01T08:53:57.000Z",
            "updated_at": "2020-09-01T09:04:02.000Z",
            "user": 1
        }
    }
}*/