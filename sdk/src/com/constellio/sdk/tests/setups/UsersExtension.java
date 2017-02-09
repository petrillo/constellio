package com.constellio.sdk.tests.setups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;

public class UsersExtension extends Users {

	public UsersExtension() {
	}

	public List<User> getUsers(String collection) {
		ArrayList<User> list = new ArrayList<User>();
		
		try {
			list.add(chuckNorrisIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}
		
		try {
			list.add(adminIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}

		try {
			list.add(aliceIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}
		
		try {
			list.add(bobIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}

		try {
			list.add(charlesIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}

		try {
			list.add(dakotaIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}

		try {
			list.add(edouardIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}

		try {
			list.add(gandalfIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}

		try {
			list.add(robinIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}

		try {
			list.add(sasquatchIn(collection));
		} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
		}
		
		return list;
	}
}
