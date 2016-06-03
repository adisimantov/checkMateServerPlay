package algo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import model.MySqlDriver;

public class EmotionsManager {

	public static boolean setLike(JsonNode likes){
		if (likes.isArray()) {
		    for (final JsonNode objNode : likes) {
		    }
		}
		
		return true;
	}
	
	public static boolean setDislike(JsonNode dislikes){
		if (dislikes.isArray()) {
		    for (final JsonNode objNode : dislikes) {
		    }
		}
		
		return true;
	}
}
