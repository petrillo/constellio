package com.constellio.app.ui.pages.management.extractors.plugin.regexPopulatorPlugin;

import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorToVOBuilder;
import com.constellio.app.ui.pages.management.extractors.plugin.MetadataPopulatorVO;
import com.constellio.model.services.records.extractions.populator.MetadataPopulator;
import com.constellio.model.services.records.extractions.populator.plugin.regexPopulatorPlugin.RegexPopulator;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Majid on 2016-06-02.
 */
public class RegexPopulatorToVOBuilder implements MetadataPopulatorToVOBuilder {
    @Override
    public MetadataPopulatorVO build(MetadataPopulator populator) {
        RegexPopulatorVO vo = null;
        if (populator instanceof RegexPopulator){
            RegexPopulator regexPopulator = (RegexPopulator) populator;
            vo = new RegexPopulatorVO();
            try {
                BeanUtils.copyProperties(vo, regexPopulator.getExtractor());
                BeanUtils.copyProperties(vo, regexPopulator.getExtractorSupplier());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return vo;
    }
}
