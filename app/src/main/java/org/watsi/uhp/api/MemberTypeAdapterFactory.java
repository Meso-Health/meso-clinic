package org.watsi.uhp.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.models.Member;

/**
 * Factory for custom serialization of Member objects
 */
public class MemberTypeAdapterFactory extends CustomizedTypeAdapterFactory<Member> {

    public MemberTypeAdapterFactory(Class<Member> customizedClass) {
        super(customizedClass);
    }

    /**
     * For development and testing, we use local servers.
     * Local servers return the paths of files (e.g. /path/to/file.jpg),
     * not fully-qualified urls (e.g. https://www.cloudfront.net/path/to/file.jpg).
     * To complete the url before storing it in our DB, we must manually append the domain name
     * (e.g. http:/192.64.x.x:5000) to the path (e.g. /path/to/file.jpg)
     * to generate the full url (e.g. http:/192.64.x:5000/path/to/file.jpg)
     */
    @Override
    protected void afterRead(JsonElement deserialized) {
        if (BuildConfig.USING_LOCAL_SERVER) {
            JsonObject jsonObject = deserialized.getAsJsonObject();
            String photoUrl = jsonObject.remove(Member.FIELD_NAME_PHOTO_URL).getAsString();
            jsonObject.addProperty(Member.FIELD_NAME_PHOTO_URL, BuildConfig.API_HOST + photoUrl);
        }
    }
}
