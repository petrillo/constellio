package com.constellio.app.modules.rm.services.borrowingServices;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_CannotBorrowActiveFolder;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsNotBorrowed;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_InvalidBorrowingDate;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class BorrowingServices {
	public static final String RGD = "RGD";
	private final RecordServices recordServices;
	private final LoggingServices loggingServices;
	private final RMSchemasRecordsServices rm;

	public BorrowingServices(String collection, ModelLayerFactory modelLayerFactory) {
		this.recordServices = modelLayerFactory.newRecordServices();
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public void borrowFolder(String folderId, LocalDate borrowingDate, LocalDate previewReturnDate, User currentUser,
			User borrowerEntered, BorrowingType borrowingType)
			throws RecordServicesException {

		Record folderRecord = recordServices.getDocumentById(folderId);
		Folder folder = rm.wrapFolder(folderRecord);
		validateCanBorrow(currentUser, folder, borrowingDate);
		setBorrowedMetadatasToFolder(folder, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime(),
				previewReturnDate,
				currentUser.getId(), borrowerEntered.getId(),
				borrowingType);
		recordServices.update(folder);
		if (borrowingType == BorrowingType.BORROW) {
			loggingServices.borrowRecord(folderRecord, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
		} else {
			loggingServices
					.consultingRecord(folderRecord, borrowerEntered, borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime());
		}

	}

	public void returnFolder(String folderId, User currentUser, LocalDate returnDate)
			throws RecordServicesException {

		Record folderRecord = recordServices.getDocumentById(folderId);
		Folder folder = rm.wrapFolder(folderRecord);
		validateCanReturnFolder(currentUser, folder);
		BorrowingType borrowingType = folder.getBorrowType();
		setReturnedMetadatasToFolder(folder);
		recordServices.update(folder);
		if (borrowingType == BorrowingType.BORROW) {
			loggingServices.returnRecord(folderRecord, currentUser, returnDate.toDateTimeAtStartOfDay().toLocalDateTime());
		}
	}

	public void validateCanReturnFolder(User currentUser, Folder folder) {
		if (currentUser.hasReadAccess().on(folder)) {
			if (folder.getBorrowed() == null || !folder.getBorrowed()) {
				throw new BorrowingServicesRunTimeException_FolderIsNotBorrowed(folder.getId());
			} else if (!currentUser.getUserRoles().contains(RGD) && !currentUser.getId()
					.equals(folder.getBorrowUserEntered())) {
				throw new BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder(currentUser.getUsername());
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
		}
	}

	public void validateCanBorrow(User currentUser, Folder folder, LocalDate borrowingDate) {

		if (currentUser.hasReadAccess().on(folder)) {
			if (FolderStatus.ACTIVE == folder.getArchivisticStatus()) {
				throw new BorrowingServicesRunTimeException_CannotBorrowActiveFolder(folder.getId());
			} else if (folder.getBorrowed() != null && folder.getBorrowed()) {
				throw new BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed(folder.getId());
			} else if (borrowingDate != null && borrowingDate.isAfter(TimeProvider.getLocalDate())) {
				throw new BorrowingServicesRunTimeException_InvalidBorrowingDate(borrowingDate);
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
		}
	}

	private void setBorrowedMetadatasToFolder(Folder folder, LocalDateTime borrowingDate, LocalDate previewReturnDate,
			String userId, String borrowerEnteredId, BorrowingType borrowingType) {
		folder.setBorrowed(true);
		folder.setBorrowDate(borrowingDate != null ? borrowingDate : TimeProvider.getLocalDateTime());
		folder.setBorrowPreviewReturnDate(previewReturnDate);
		folder.setBorrowUser(userId);
		folder.setBorrowUserEntered(borrowerEnteredId);
		folder.setBorrowType(borrowingType);
		folder.setAlertUsersWhenAvailable(new ArrayList<String>());
	}

	private void setReturnedMetadatasToFolder(Folder folder) {
		folder.setBorrowed(null);
		folder.setBorrowDate(null);
		folder.setBorrowPreviewReturnDate(null);
		folder.setBorrowUser(null);
		folder.setBorrowUserEntered(null);
		folder.setBorrowType(null);
	}

	public String validateBorrowingInfos(String userId, LocalDate borrowingDate, LocalDate previewReturnDate,
			BorrowingType borrowingType, LocalDate returnDate) {
		String errorMessage = null;
		if (borrowingDate == null) {
			borrowingDate = TimeProvider.getLocalDate();
		} else {
			if (borrowingDate.isAfter(TimeProvider.getLocalDate())) {
				errorMessage = "BorrowingServices.invalidBorrowingDate";
			}
		}
		if (borrowingType == null) {
			errorMessage = "BorrowingServices.invalidBorrowingType";
			return errorMessage;
		}
		if (StringUtils.isBlank(userId) || userId == null) {
			errorMessage = "BorrowingServices.invalidBorrower";
			return errorMessage;
		}
		if (previewReturnDate != null) {
			if (previewReturnDate.isBefore(borrowingDate)) {
				errorMessage = "BorrowingServices.invalidPreviewReturnDate";
				return errorMessage;
			}
		} else {
			errorMessage = "BorrowingServices.invalidPreviewReturnDate";
			return errorMessage;
		}
		if (returnDate != null) {
			return validateReturnDate(returnDate, borrowingDate);
		}
		return errorMessage;
	}

	public String validateReturnDate(LocalDate returnDate, LocalDate borrowingDate) {
		String errorMessage = null;
		if (returnDate == null) {
			errorMessage = "BorrowingServices.invalidReturnDate";
		} else if (borrowingDate != null && (returnDate.isAfter(TimeProvider.getLocalDate()) || returnDate.isBefore(borrowingDate))) {
			errorMessage = "BorrowingServices.invalidReturnDate";
		}
		return errorMessage;
	}
	
}