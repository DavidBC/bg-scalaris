package TestDS;

import com.google.gson.*;
import de.zib.scalaris.ErlangValue;
import de.zib.scalaris.TransactionSingleOp;
import edu.usc.bg.base.ByteIterator;
import edu.usc.bg.base.StringByteIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Type;
import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TestDSClient.class)
public class TestDSClientTest {
    private static final String FRIEND_COUNT = "friendcount";
    private static final String RESOURCE_COUNT = "resourcecount";
    private static final String PENDING_COUNT = "pendingcount";

    TestDSClient testDSClient;
    @Mock
    TransactionSingleOp transactionSingleOp;
    @Mock
    ErlangValue erlangValue;

    @Before
    public void setUp() throws Exception {
        testDSClient = new TestDSClient();
        testDSClient.init();

        whenNew(TransactionSingleOp.class).withAnyArguments().thenReturn(transactionSingleOp);

        doNothing().when(transactionSingleOp).write(anyString(), anyObject());
        doReturn(erlangValue).when(transactionSingleOp).read(anyString());

        doReturn("{\"a\":\"test\"}").when(erlangValue).stringValue();
    }

    @Test
    public void testHashMapToGson() throws Exception {
        final HashMap<String, ByteIterator> forType = new HashMap<>();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(forType.getClass(), new JsonSerializer<HashMap<String, ByteIterator>>() {
                    @Override
                    public JsonElement serialize(HashMap<String, ByteIterator> hashMap, Type type,
                                                 JsonSerializationContext context) {
                        JsonObject jsonObject = new JsonObject();
                        hashMap.forEach((k, v) -> jsonObject.add(k, new JsonPrimitive(v.toString())));
                        return jsonObject;
                    }
                }).create();

        ByteIterator byteIterator = new StringByteIterator("test");
        HashMap<String, ByteIterator> values = new HashMap<>();
        values.put("a", byteIterator);

        String jsonString = gson.toJson(values);
        assertThat(jsonString, is("{\"a\":\"test\"}"));

        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsonString);
        assertThat(jsonElement.toString(), is("{\"a\":\"test\"}"));
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        assertThat(jsonObject.toString(), is("{\"a\":\"test\"}"));
        JsonElement pendingFriendsElement = jsonObject.get("pendingFriends");
        assertThat(pendingFriendsElement, nullValue());
        JsonArray jsonArray = jsonObject.getAsJsonArray("pendingFriends");
        assertThat(jsonArray, nullValue());
    }

    @Test
    public void testErlangValue() throws Exception {
        String jsonString = "{\"a\":\"test\"}";
        ErlangValue erlangValue = new ErlangValue(jsonString);
        assertThat(erlangValue.stringValue(), is(jsonString));
    }

    @Test
    public void testInsertEntity() throws Exception {
        String entitySet = "users";
        String entityPK = "1";
        HashMap<String, ByteIterator> values = new HashMap<>();
        values.put("userid", new StringByteIterator(""));
        values.put("username", new StringByteIterator(""));
        values.put("pw", new StringByteIterator(""));
        values.put("fname", new StringByteIterator(""));
        values.put("lname", new StringByteIterator(""));
        values.put("gender", new StringByteIterator(""));
        values.put("dob", new StringByteIterator(""));
        values.put("jdate", new StringByteIterator(""));
        values.put("ldate", new StringByteIterator(""));
        values.put("address", new StringByteIterator(""));
        values.put("email", new StringByteIterator(""));
        values.put("tel", new StringByteIterator(""));

        int result = testDSClient.insertEntity(entitySet, entityPK, values, false);
        assertThat(result, is(0));

        entitySet = "resource";
        values.put("mid", new StringByteIterator(""));
        values.put("creatorid", new StringByteIterator(""));
        values.put("rid", new StringByteIterator(""));
        values.put("modifierid", new StringByteIterator(""));
        values.put("timestamp", new StringByteIterator(""));
        values.put("type", new StringByteIterator(""));
        values.put("content", new StringByteIterator(""));

        result = testDSClient.insertEntity(entitySet, entityPK, values, false);
        assertThat(result, is(0));
    }

    @Test
    public void testViewProfile() throws Exception {
        int requesterID = 1;
        int profileOwnerID = 1;
        HashMap<String, ByteIterator> result = new HashMap<>();

        doReturn("{\"userid\":\"\",\"username\":\"\",\"pw\":\"\",\"fname\":\"\",\"lname\":\"\",\"gender\":\"\"," +
                "\"dob\":\"\",\"jdate\":\"\",\"ldate\":\"\",\"address\":\"\",\"email\":\"\",\"tel\":\"\"," +
                "\"confirmedFriends\":[\"3\",\"4\"],\"pendingFriends\":[\"5\"],\"resources\":[\"1\"]}")
                .when(erlangValue).stringValue();

        testDSClient.viewProfile(requesterID, profileOwnerID, result, false, false);
        assertTrue(result.containsKey(FRIEND_COUNT));
        assertTrue(result.containsKey(RESOURCE_COUNT));
        assertTrue(result.containsKey(PENDING_COUNT));

        doReturn("{\"userid\":\"\",\"username\":\"\",\"pw\":\"\",\"fname\":\"\",\"lname\":\"\",\"gender\":\"\"," +
                "\"dob\":\"\",\"jdate\":\"\",\"ldate\":\"\",\"address\":\"\",\"email\":\"\",\"tel\":\"\"," +
                "\"confirmedFriends\":[],\"pendingFriends\":[\"5\"],\"resources\":[]}")
                .when(erlangValue).stringValue();

        testDSClient.viewProfile(requesterID, profileOwnerID, result, false, false);
        assertTrue(result.containsKey(FRIEND_COUNT));
        assertTrue(result.containsKey(RESOURCE_COUNT));
        assertTrue(result.containsKey(PENDING_COUNT));

        doReturn("{\"userid\":\"\",\"username\":\"\",\"pw\":\"\",\"fname\":\"\",\"lname\":\"\",\"gender\":\"\"," +
                "\"dob\":\"\",\"jdate\":\"\",\"ldate\":\"\",\"address\":\"\",\"email\":\"\",\"tel\":\"\"," +
                "\"confirmedFriends\":[\"3\",\"4\"],\"pendingFriends\":[],\"resources\":[]}")
                .when(erlangValue).stringValue();

        testDSClient.viewProfile(requesterID, profileOwnerID, result, false, false);
        assertTrue(result.containsKey(FRIEND_COUNT));
        assertTrue(result.containsKey(RESOURCE_COUNT));
        assertTrue(result.containsKey(PENDING_COUNT));
    }

    @Test
    public void testListFriends() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testViewFriendReq() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testAcceptFriend() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testRejectFriend() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testInviteFriend() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testViewTopKResources() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testGetCreatedResources() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testViewCommentOnResource() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testPostCommentOnResource() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testDelCommentOnResource() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testThawFriendship() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testGetInitialStats() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testCreateFriendship() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testCreateSchema() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testQueryPendingFriendshipIds() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testQueryConfirmedFriendshipIds() throws Exception {
        assertTrue(true);
    }
}