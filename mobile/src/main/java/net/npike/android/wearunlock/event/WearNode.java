package net.npike.android.wearunlock.event;

import com.google.android.gms.wearable.Node;

/**
 * Created by npike on 6/30/14.
 */
public class WearNode {
    private String mDisplayName = "";
    private String mId = "";

    public WearNode(String displayName, String id) {
        mDisplayName = displayName;
        mId = id;
    }

    public WearNode(Node node) {
        mDisplayName = node.getDisplayName();
        mId = node.getId();
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }
}
