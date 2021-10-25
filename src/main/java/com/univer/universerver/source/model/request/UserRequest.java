package com.univer.universerver.source.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    private long id;
    private String nickname;
    private String introduce;
    private String[] userImages;



}
