package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.db.Database;
import play.db.Databases;

import com.restfb.types.FacebookType;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class MySqlDriver {
    private static final Map<String, String> params;
    static
    {
    	params = new HashMap<String, String>();
    	params.put("user", "admina45B3wQ");
    	params.put("password", "t9XTASkPXhYv");
    	params.put("useUnicode", "true");
    	params.put("characterEncoding", "UTF-8");
    }
	
	public static final Database database = Databases.createFrom(
			"com.mysql.jdbc.Driver",
			"jdbc:mysql://127.0.0.1:3306/checkmatep",
			params
	);


	// TODO: check if not used at anything else!!!!!
	public static Type getGoogleType(String googleType) {
		Connection conn = database.getConnection();

		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Type t = null;
		try {
			String s = " SELECT g.goog_type_id, g.goog_type_name " + " FROM google_types g "
					+ " WHERE g.goog_type_name = ? ";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, googleType);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				t = new Type();
				t.setId(rs.getInt("goog_type_id"));
				t.setName(rs.getString("goog_type_name"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return t;

	}

	public static Type getFacebookType(String facebookType) {

		Connection conn = database.getConnection();

		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Type t = null;
		try {
			String s = " SELECT f.fb_type_id, f.fb_type_name " + " FROM facebook_types f "
					+ " WHERE f.fb_type_name = ? ";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, facebookType);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				t = new Type();
				t.setId(rs.getInt("fb_type_id"));
				t.setName(rs.getString("fb_type_name"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return t;

	}

	public static Type getGoogleTypeFromFacebook(String facebookType) {
		Connection conn = database.getConnection();

		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Type t = null;
		try {
			String s = " SELECT g.goog_type_id, g.goog_type_name							   "
					+ " FROM google_types g                                                    "
					+ " JOIN google_to_facebook fg ON g.goog_type_id = fg.goog_type_id   "
					+ " JOIN facebook_types f ON f.fb_type_id = fg.fb_type_id                  "
					+ " WHERE f.fb_type_name = ?                                               ";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, facebookType);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				t = new Type();
				t.setId(rs.getInt("goog_type_id"));
				t.setName(rs.getString("goog_type_name"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return t;

	}

	public static int getDislikeCountByGoogType(int googTypeId, String userId) {

		Connection conn = database.getConnection();

		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		int count = 0;
		try {
			String s = "SELECT COUNT(*) FROM emotions WHERE user_id = ? and goog_type_id = ? and  'like_ind' = 0 and date > (CURRENT_TIMESTAMP - 30)";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setInt(1, googTypeId);
			preparedStatement.setString(2, userId);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return count;
	}

	public static int getDislikeCountByPlace(String placeId, String userId) {

		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		int count = 0;
		try {
			String s = "SELECT COUNT(*) FROM emotions WHERE user_id = ? and goog_place_id = ? and 'like_ind' = 0 and date BETWEEN NOW() - INTERVAL 30 DAY AND NOW()";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, userId);
			preparedStatement.setString(2, placeId);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return count;
	}

	public static boolean setDislike(String userId, String placeId, int googleType) {
		return setEmotion(userId, '0', placeId, googleType);
	}

	public static boolean setLike(String userId, String placeId, int googleType) {
		return setEmotion(userId, '1', placeId, googleType);
	}

	public static User getUser(String userId) {
		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		User user = null;
		try {
 			String s = " SELECT u.user_id, u.token"
					 + " FROM users u"
					 + " WHERE user_id = ?";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, userId);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				user = new User();
				user.setUserId(rs.getString("user_id"));
				user.setToken(rs.getString("token"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return user;
	}
	
	public static boolean setUser(String userId, String token, Integer age, String gender) {
		Connection conn = database.getConnection();
		boolean result = true;
		PreparedStatement preparedStatement = null;
		int count = 0;
		try {
			String s = "INSERT INTO users (user_id, token, age, gender) "
					+ " VALUES (?, ?, ?, ?)";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, userId);
			preparedStatement.setString(2, token);
			if (age != null) {
				preparedStatement.setInt(3, age);
			} else {
				preparedStatement.setNull(3, Types.INTEGER);
			}
			preparedStatement.setString(4, gender);
			count = preparedStatement.executeUpdate();
			if (count == 0) {
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public static boolean setEmotion(String userId, char like, String placeId, int googleType) {

		Connection conn = database.getConnection();
		boolean result = true;
		PreparedStatement preparedStatement = null;
		int count = 0;
		try {
			String s = "INSERT INTO emotions (user_id, date, like_ind, goog_place_id, goog_type_id) "
					+ " VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, userId);
			preparedStatement.setString(2, String.valueOf(like));
			preparedStatement.setString(3, placeId);
			preparedStatement.setInt(4, googleType);
			count = preparedStatement.executeUpdate();
			if (count == 0) {
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	public static List<Type> getGoogleTypesByInterest(int interest) {
		
		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Type t = null;
		List<Type> googleTypes = new ArrayList<Type>();
		try {
 			String s = " SELECT t.goog_type_id, t.goog_type_name"
					 + " FROM google_to_interest i"
					 + " JOIN google_types t on t.goog_type_id = i.goog_type_id"
					 + " WHERE interest_type = ?";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setInt(1, interest);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				t = new Type();
				t.setId(rs.getInt("goog_type_id"));
				t.setName(rs.getString("goog_type_name"));
				googleTypes.add(t);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return googleTypes;
	}
	
	public static List<Type> getFacebookTypesByInterest(int interest) {

		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Type t = null;
		List<Type> facebookTypes = new ArrayList<Type>();
		try {
 			String s = " SELECT t.fb_type_id, t.fb_type_name"
					 + " FROM facebook_to_interest i"
					 + " JOIN facebook_types t on t.fb_type_id = i.fb_type_id"
					 + " WHERE interest_type = ?";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setInt(1, interest);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				t = new Type();
				t.setId(rs.getInt("fb_type_id"));
				t.setName(rs.getString("fb_type_name"));
				facebookTypes.add(t);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return facebookTypes;
	}
			
	public static List<Interest> getInterestsByGoogleType(int googleType) {

		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Interest t = null;
		List<Interest> interestTypes = new ArrayList<Interest>();
		try {
 			String s = " SELECT i.interest_type,t.interest_name"
					 + " FROM google_to_interest i"
					 + " JOIN interests t ON t.interest_id = i.interest_type"
					 + " WHERE goog_type_id = ?";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setInt(1, googleType);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				t = new Interest();
				t.setId(rs.getInt("interest_type"));
				t.setName(rs.getString("interest_name"));
				interestTypes.add(t);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return interestTypes;
	}
	
	public static List<Interest> getInterestsByFacebookType(String facebookType) {

		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Interest t = null;
		List<Interest> interestTypes = new ArrayList<Interest>();
		try {
 			String s = " SELECT i.interest_type,t.interest_name"
					 + " FROM facebook_to_interest i"
					 + " JOIN interests t ON t.interest_id = i.interest_type"
					 + " JOIN facebook_types f ON f.fb_type_id = i.fb_type_id"
					 + " WHERE fb_type_name = ?";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, facebookType);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				t = new Interest();
				t.setId(rs.getInt("interest_type"));
				t.setName(rs.getString("interest_name"));
				interestTypes.add(t);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return interestTypes;
	}
	
    private static String createGoogleTypesByInterestWithRateQuery(int facebookTypeLen,int googleTypeLen) {
		String s = 
		"select gi.goog_type_id,gt.goog_type_name, (interests.count +1) * IFNULL(power(1.2,e.like), 1) * IFNULL(power(1/1.2,e.dislike), 1) as 'rate' " +
		"from google_to_interest gi JOIN " +
		"	 google_types gt USING (goog_type_id) " +
		"	 JOIN " +
		"     (select fi.interest_type,count(*) count " +
		"     from facebook_to_interest fi " +
		"     join facebook_types ft on ft.fb_type_id = fi.fb_type_id ";
		StringBuilder queryBuilder = new StringBuilder("where ft.fb_type_name in (");
        for( int i = 0; i< facebookTypeLen; i++){
            queryBuilder.append(" ?");
            if(i != facebookTypeLen -1) queryBuilder.append(",");
        }
        queryBuilder.append(") ");
        s += queryBuilder.toString();
        
		String s2 = "     group by fi.interest_type) interests USING (interest_type) " +
		"     LEFT JOIN " +
		"     (select goog_type_id, COUNT(CASE WHEN like_ind = 1 then 1 ELSE NULL END) as 'like', COUNT(CASE WHEN like_ind = 0 then 1 ELSE NULL END) as 'dislike' " +
		"     from emotions e " +
		"     where e.user_id = ? " +
		"     group by goog_type_id) e USING (goog_type_id) " ;
			
        queryBuilder = new StringBuilder("where gi.goog_type_id not in (");
        for( int i = 0; i< googleTypeLen; i++){
            queryBuilder.append(" ?");
            if(i != googleTypeLen -1) queryBuilder.append(",");
        }
        queryBuilder.append(") ");
        s2 += queryBuilder.toString();
        
        return s+s2;
    }
    
	public static List<Type> getGoogleTypesByInterestWithRate(List<String> facebookTypes,List<Integer> googleTypes,String userID) {

		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Type t = null;
		List<Type> types = new ArrayList<Type>();
		try {
 			String s = createGoogleTypesByInterestWithRateQuery(facebookTypes.size(),googleTypes.size());
			preparedStatement = conn.prepareStatement(s);
			
			for(int i = 1; i <= facebookTypes.size(); i++){
				preparedStatement.setString(i, facebookTypes.get(i- 1));
			}
			
			preparedStatement.setString(facebookTypes.size() + 1, userID);
			
			for(int i = facebookTypes.size() + 2,j=0; j < googleTypes.size(); i++,j++){
				preparedStatement.setInt(i, googleTypes.get(j));
			}
			
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				t = new Type();
				t.setId(rs.getInt("goog_type_id"));
				t.setName(rs.getString("goog_type_name"));
				t.setRate(rs.getInt("rate"));
				types.add(t);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return types;
	}
	
	public List<Type> getTypesByFacebookInterests(Iterable<String> inSubSelect, Iterable<String> notIn){
		
		return null;
	}
	
	public static boolean saveCheckin(String user_id,
							String checkin_id,
							Timestamp create_time,
							String place_id,
							String name,
							double latitude,
							double longitude,
							String street,
							String city,
							String country,
							String zip,
							String main_category,
							Integer checkin_count,
							Long likes,
							String price_range,
							String goog_place_id,
							List<FacebookType> types) {
		
		Connection conn = database.getConnection();
		boolean result = true;
		int count = 0;
		PreparedStatement preparedStatement = null;
		
		try {
			String s = "INSERT INTO checkins (user_id,checkin_id,created_time,place_id,name,latitude,longitude,street,city,country,zip,main_category,checkin_count,likes,price_range,price_range_code, goog_place_id) "
					+ " VALUES (?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, user_id);
			preparedStatement.setString(2, checkin_id);
			preparedStatement.setTimestamp(3, create_time);
			preparedStatement.setString(4, place_id);
			preparedStatement.setNString(5, name);
			preparedStatement.setDouble(6, latitude);
			preparedStatement.setDouble(7, longitude);
			preparedStatement.setNString(8, street);
			preparedStatement.setNString(9, city);
			preparedStatement.setNString(10, country);
			preparedStatement.setString(11, zip);
			preparedStatement.setString(12, main_category);
			if (checkin_count == null) {
				preparedStatement.setNull(13, Types.BIGINT);
			} else {
				preparedStatement.setLong(13, checkin_count);
			}
			if (likes == null) {
				preparedStatement.setNull(14, Types.BIGINT);
			} else {
				preparedStatement.setLong(14, likes);
			}
			preparedStatement.setString(15, price_range);
			int price_range_code = 0;
			if (price_range != null) {
				if (price_range.contains("$$$")) {
					price_range_code = 3;
				} else if (price_range.contains("$$")) {
					price_range_code = 2;
				} else if (price_range.contains("$")){
					price_range_code = 1;
				}
			}
			preparedStatement.setInt(16, price_range_code);
			preparedStatement.setString(17, goog_place_id);
			count = preparedStatement.executeUpdate();
			if (count == 0) {
				result = false;
			}
			preparedStatement.close();
			
			s = "INSERT INTO checkin_types (checkin_id,type_id,name) "
				+ " VALUES (?, ?, ?)";
			preparedStatement = conn.prepareStatement(s);

			for (FacebookType type :types) {
				preparedStatement.clearParameters();
				preparedStatement.setString(1, checkin_id);
				preparedStatement.setString(2, type.getId());
				preparedStatement.setString(3, type.getType());
				count = preparedStatement.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	
	public static List<User> getUsers() {
		
		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		User u = null;
		List<User> users = new ArrayList<User>();
		try {
 			String s = " SELECT u.user_id, u.token, u.age, u.gender"
					 + " FROM users u";
			preparedStatement = conn.prepareStatement(s);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				u = new User();
				u.setUserId(rs.getString("user_id"));
				u.setToken(rs.getString("token"));
				u.setAge(rs.getInt("age"));
				if (rs.getString("gender") != null){
					u.setGender(rs.getString("gender").charAt(0));
				}
				users.add(u);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return users;
	}
	
	public static Map<Interest,Integer> getUserInterestCount(String user_id) {
		
		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Interest u = null;
		Integer count = 0;
		Map<Interest,Integer> interestCount = new HashMap<Interest, Integer>();
		try {
			String s = 	"SELECT  i.interest_id,i.interest_name, count(fi.interest_type) as count" + 
						" FROM checkins c JOIN checkin_types t ON c.checkin_id = t.checkin_id and c.user_id = ?" +
						" JOIN facebook_types f ON f.fb_type_name = t.name" +
						" JOIN facebook_to_interest fi ON fi.fb_type_id = f.fb_type_id" +
						" RIGHT JOIN interests i ON i.interest_id = fi.interest_type" +
						" GROUP BY i.interest_id" +
						" ORDER BY interest_id"
						;
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, user_id);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				u = new Interest();
				u.setId(rs.getInt("interest_id"));
				u.setName(rs.getString("interest_name"));
				count = rs.getInt("count");
				interestCount.put(u, count);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return interestCount;
	}
	
	public static Map<Integer,Integer> getUserPriceRangeCount(String user_id) {
		
		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Integer id = null;
		Integer count = 0;
		Map<Integer,Integer> priceCount = new HashMap<Integer, Integer>();
		try {
			String s = 	"SELECT r.id, count(c.price_range_code) as count" +
						" FROM checkins c " +
						" RIGHT JOIN price_ranges r ON r.id = c.price_range_code and  c.user_id = ? and r.id > 0" +
						" GROUP BY r.id" + 
						" ORDER BY id";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, user_id);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				id = rs.getInt("id");
				count = rs.getInt("count");
				priceCount.put(id, count);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return priceCount;
	}
	
	public static boolean insertOrUpdateSimilarity(String first_user_id,String sec_user_id, double similarity) {
		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		boolean result = false;
		try {
			String s = "SELECT 1 FROM user_similarity WHERE first_user_id = ? and sec_user_id = ?";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, first_user_id);
			preparedStatement.setString(2, sec_user_id);
			rs = preparedStatement.executeQuery();
			if (rs.first()) {
				result = updateSimlarity(first_user_id, sec_user_id, similarity);
			} else {
				preparedStatement.close();
				rs.close();
				preparedStatement = conn.prepareStatement(s);
				preparedStatement.setString(1, sec_user_id);
				preparedStatement.setString(2, first_user_id);
				rs = preparedStatement.executeQuery();
				
				if (rs.first()) {
					result = updateSimlarity(sec_user_id, first_user_id, similarity);
				} else {
					result = insertSimlarity(first_user_id, sec_user_id, similarity);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
	
	public static boolean insertSimlarity(String first_user_id,String sec_user_id, double similarity) {

		Connection conn = database.getConnection();
		boolean result = true;
		PreparedStatement preparedStatement = null;
		int count = 0;
		try {
			String s = "INSERT INTO user_similarity (first_user_id, sec_user_id, total) "
					 + " VALUES (?, ?, ?)";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, first_user_id);
			preparedStatement.setString(2, sec_user_id);
			preparedStatement.setDouble(3, similarity);
			count = preparedStatement.executeUpdate();
			if (count == 0) {
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static boolean updateSimlarity(String first_user_id,String sec_user_id, double similarity) {

		Connection conn = database.getConnection();
		boolean result = true;
		PreparedStatement preparedStatement = null;
		int count = 0;
		try {
			String s = "UPDATE user_similarity SET total = ? WHERE first_user_id = ? AND sec_user_id = ?";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setDouble(1, similarity);
			preparedStatement.setString(2, first_user_id);
			preparedStatement.setString(3, sec_user_id);

			count = preparedStatement.executeUpdate();
			if (count == 0) {
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static List<String> getSimilarPlaces(String user_id, Location curr_location) {

		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		List<String> locations = new ArrayList<String>();
		try {
 			String s =  "SELECT goog_place_id, " +
	 					"   111.1111 *"+
	 					"    DEGREES(ACOS(COS(RADIANS(latitude))"+
	 					"         * COS(RADIANS(?))"+
	 					"         * COS(RADIANS(longitude - ?))"+
	 					"         + SIN(RADIANS(latitude))"+
	 					"         * SIN(RADIANS(?)))) AS distance_in_km"+
	 					" FROM checkins "+
	 					" WHERE user_id = ? and goog_place_id is not null"+
	 					" HAVING distance_in_km < 3"+
	 					" ORDER BY likes DESC , checkin_count DESC" +
	 					" LIMIT 0,2";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, curr_location.lat);
			preparedStatement.setString(2, curr_location.lng);
			preparedStatement.setString(3, curr_location.lat);
			preparedStatement.setString(4, user_id);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				locations.add(rs.getString("goog_place_id"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null){
					rs.close();
				}
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return locations;
	}
	
	public static List<String> getSimilarUsers(String user_id) {
		Connection conn = database.getConnection();
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		String id = null;
		Double score = 0.0;
		List<String> similarUsers = new ArrayList<String>();
		try {
			String s = 	"SELECT other_user FROM" +
						" (SELECT total,sec_user_id as other_user FROM user_similarity WHERE first_user_id = ? " +
						" UNION " +
						" SELECT total,first_user_id as other_user FROM user_similarity WHERE sec_user_id = ?) a" +
						" ORDER BY total DESC";
			preparedStatement = conn.prepareStatement(s);
			preparedStatement.setString(1, user_id);
			preparedStatement.setString(2, user_id);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				id = rs.getString("other_user");
				similarUsers.add(id);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				preparedStatement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return similarUsers;
	}
	
	
	public static void calcSimilarities() throws Exception {
		List<User> users = MySqlDriver.getUsers();
		Map<Integer,Integer> first_price_data = null;
		Map<Integer,Integer> sec_price_data = null;
		Map<Interest,Integer> first_type_data = null;
		Map<Interest,Integer> sec_type_data = null;
		List<Integer> first_data_vector = null;
		List<Integer> sec_data_vector = null;
		
		for (int i=0;i<users.size();i++) {
			first_price_data = MySqlDriver.getUserPriceRangeCount( users.get(i).getUserId());
			first_type_data = MySqlDriver.getUserInterestCount( users.get(i).getUserId());
			first_data_vector = new ArrayList<Integer>();
			first_data_vector.add(users.get(i).getAgeNotNull());
			first_data_vector.add(users.get(i).getGenderCode());
			first_data_vector.addAll(first_type_data.values());
			first_data_vector.addAll(first_price_data.values());
			
			for (int j=i+1;j<users.size();j++){
				sec_price_data = MySqlDriver.getUserPriceRangeCount( users.get(j).getUserId());
				sec_type_data = MySqlDriver.getUserInterestCount( users.get(j).getUserId());
				sec_data_vector = new ArrayList<Integer>();
				sec_data_vector.add(users.get(j).getAgeNotNull());
				sec_data_vector.add(users.get(j).getGenderCode());
				sec_data_vector.addAll(sec_type_data.values());
				sec_data_vector.addAll(sec_price_data.values());

				double similarity = 
						cosineSimilarity(first_data_vector.toArray(new Integer[first_data_vector.size()]), sec_data_vector.toArray(new Integer[sec_data_vector.size()]));

				MySqlDriver.insertOrUpdateSimilarity(users.get(i).getUserId(),users.get(j).getUserId(),similarity);
			}
		}
	}
	
	public static double cosineSimilarity(Integer[] vectorA, Integer[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    
	    if (normA == 0 || normB == 0) {
	    	return 0;
	    }
	    
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
}