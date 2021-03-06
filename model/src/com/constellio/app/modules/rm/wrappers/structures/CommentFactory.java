package com.constellio.app.modules.rm.wrappers.structures;

import java.util.StringTokenizer;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;

//AFTER : Move in core
public class CommentFactory implements StructureFactory {

	private static final String NULL = "~null~";

	@Override
	public ModifiableStructure build(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, ":");

		Comment comment = new Comment();
		comment.userId = readString(stringTokenizer);
		comment.username = readString(stringTokenizer);
		comment.setDateTime(readLocalDateTime(stringTokenizer));
		comment.setMessage(readString(stringTokenizer));
		comment.dirty = false;
		return comment;
	}

	@Override
	public String toString(ModifiableStructure structure) {

		Comment comment = (Comment) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, "" + comment.getUserId() == null ?
				NULL :
				comment.getUserId());
		writeString(stringBuilder, "" + comment.getUsername() == null ?
				"" :
				comment.getUsername());
		if (comment.getDateTime() != null) {
			writeString(stringBuilder, comment.getDateTime().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		} else {
			writeString(stringBuilder, NULL);
		}
		writeString(stringBuilder, "" + comment.getMessage() == null ?
				NULL :
				comment.getMessage());
		return stringBuilder.toString();
	}

	private String readString(StringTokenizer stringTokenizer) {
		String value = stringTokenizer.nextToken();
		if (NULL.equals(value)) {
			return null;
		} else {
			return value.replace("~~~", ":");
		}
	}

	private void writeString(StringBuilder stringBuilder, String value) {
		if (stringBuilder.length() != 0) {
			stringBuilder.append(":");
		}
		if (value == null) {
			stringBuilder.append(NULL);
		} else {
			stringBuilder.append(value.replace(":", "~~~"));
		}
	}

	private LocalDateTime readLocalDateTime(StringTokenizer stringTokenizer) {
		String localDateTime = readString(stringTokenizer);
		return localDateTime == null ? null : LocalDateTime.parse(localDateTime);
	}

}
