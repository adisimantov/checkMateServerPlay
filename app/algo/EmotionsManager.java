package algo;

import model.MySqlDriver;

import com.fasterxml.jackson.databind.JsonNode;

public class EmotionsManager {

	public static boolean sendEmotions(JsonNode obj) {

		JsonNode emotions = obj.findPath("EMOTIONS");
		String userId = obj.findPath("USER_ID").asText();

		try {
			if (emotions.isArray()) {
				for (JsonNode jsonNode : emotions) {
					char like = jsonNode.findPath("ACTION").asText().charAt(0);
					String placeId = jsonNode.findPath("PLACE_ID").asText();
					int googleType = jsonNode.findPath("GOOG_TYPE").asInt();
					MySqlDriver.setEmotion(userId, like, placeId, googleType);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
