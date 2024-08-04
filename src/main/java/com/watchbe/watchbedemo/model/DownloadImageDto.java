package com.watchbe.watchbedemo.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadImageDto {
    private byte[] imageData;
}
