package com.example.clientSide.utils;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GetListOfUsers {
    private List<ApplicationUserRequest> data;
}