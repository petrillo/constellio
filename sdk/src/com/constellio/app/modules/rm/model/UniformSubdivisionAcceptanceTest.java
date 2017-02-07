package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by Constellio on 2017-02-07.
 */
public class UniformSubdivisionAcceptanceTest extends ConstellioTest {

    RMTestRecords records = new RMTestRecords(zeCollection);
    RecordServices recordServices;
    Users users = new Users();

    @Before
    public void setUp()
            throws Exception {
        prepareSystem(
                withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users).withFoldersAndContainersOfEveryStatus()
        );

        recordServices = getModelLayerFactory().newRecordServices();
    }

    @Test
    public void givenUniformSubdivisionReferencedInAFolderThenCannotBeDeleted() throws Exception {
        recordServices.update(records.getFolder_A01().setUniformSubdivisionEntered(records.subdivId_1));

        try {
            recordServices.physicallyDeleteNoMatterTheStatus(records.getUniformSubdivision1().getWrappedRecord(), users.adminIn(zeCollection), new RecordPhysicalDeleteOptions());
            fail("Did not throw exception");
        } catch (Exception e) {

        }

        recordServices.update(records.getFolder_A01().setUniformSubdivisionEntered((String) null));
        recordServices.physicallyDeleteNoMatterTheStatus(records.getUniformSubdivision1().getWrappedRecord(), users.adminIn(zeCollection), new RecordPhysicalDeleteOptions());

    }
}
