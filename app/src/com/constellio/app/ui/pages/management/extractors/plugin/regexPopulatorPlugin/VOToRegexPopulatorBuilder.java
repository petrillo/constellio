package com.constellio.app.ui.pages.management.extractors.plugin.regexPopulatorPlugin;

import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorVO;
import com.constellio.app.ui.pages.management.extractors.plugin.VOToMetadataPopulatorBuilder;
import com.constellio.model.services.records.extractions.populator.MetadataPopulator;
import com.constellio.model.services.records.extractions.populator.plugin.regexPopulatorPlugin.RegexPopulator;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Majid on 2016-06-02.
 */
public class VOToRegexPopulatorBuilder implements VOToMetadataPopulatorBuilder{
    @Override
    public MetadataPopulator build(MetadataPopulatorVO metadataPopulatorVO) {
        RegexPopulator populator = null;
        if (metadataPopulatorVO instanceof RegexPopulatorVO){
            RegexPopulatorVO regexPopulatorVO = (RegexPopulatorVO) metadataPopulatorVO;
            populator = new RegexPopulator();
            try {
                BeanUtils.copyProperties(populator.getExtractor(), metadataPopulatorVO);
                BeanUtils.copyProperties(populator.getExtractorSupplier(), metadataPopulatorVO);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return populator;
    }
}
