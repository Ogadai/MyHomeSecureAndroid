package com.ogadai.ogadai_secure;

import java.io.IOException;

/**
 * Created by alee on 16/02/2016.
 */
public interface IServerRequest {
    String get(String address) throws IOException;
}
