package com.ogadai.ogadai_secure;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;

/**
 * Created by alee on 11/02/2016.
 */
public class UpdateStatesMessage extends MessageBase {
    @SerializedName("States")
    private List<StateItem> mStates;

    public List<StateItem> getStates() { return mStates; }
    public final void setStates(List<StateItem> states) { mStates = states; }

    public static UpdateStatesMessage FromJSON(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, UpdateStatesMessage.class);
    }
}
