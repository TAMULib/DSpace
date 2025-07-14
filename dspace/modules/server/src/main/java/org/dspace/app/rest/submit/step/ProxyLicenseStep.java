package org.dspace.app.rest.submit.step;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.atteo.evo.inflector.English;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.ErrorRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.DataLicense;
import org.dspace.app.rest.model.step.UploadBitstreamRest;
import org.dspace.app.rest.repository.WorkspaceItemRestRepository;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.rest.submit.UploadableStep;
import org.dspace.app.rest.submit.factory.PatchOperationFactory;
import org.dspace.app.rest.submit.factory.impl.PatchOperation;
import org.dspace.app.rest.utils.ProxyLicenseUtils;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.web.multipart.MultipartFile;

/**
 * TAMU Customization
 * 
 * Proxy License step for DSpace Spring Rest.
 */
public class ProxyLicenseStep extends LicenseStep implements UploadableStep {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ProxyLicenseStep.class);

    private static final String LICENSE_STEP_SELECTED_OPERATION_ENTRY = "selected";

    private static final String DCTERMS_RIGHTSDATE = "dcterms.accessRights";
    private static final String DCTERMS_ALTERNATIVE = "dcterms.alternative";

    @Override
    public boolean isExclusiveMatchingStepId() {
        return true;
    }

    @Override
    public DataLicense getData(SubmissionService submissionService, InProgressSubmission obj,
            SubmissionStepConfig config)
        throws Exception {

        DataLicense result = new DataLicense();

        Bitstream licenseBitstream = null;
        Bitstream proxyBitstream = null;

        List<Bundle> licenseBundles = itemService.getBundles(obj.getItem(), Constants.LICENSE_BUNDLE_NAME);

        if (licenseBundles.size() > 0) {
            for (Bitstream bitstream : licenseBundles.get(0).getBitstreams()) {
                if (bitstream.getName().equals(Constants.LICENSE_BITSTREAM_NAME)) {
                    licenseBitstream = bitstream;
                } else if (bitstream.getName().startsWith(ProxyLicenseUtils.PROXY_LICENSE_NAME)) {
                    proxyBitstream = bitstream;
                }
            }
        }

        List<UploadBitstreamRest> files = new ArrayList<>();

        if (licenseBitstream != null) {
            String selected = bitstreamService.getMetadata(licenseBitstream, DCTERMS_ALTERNATIVE);
            result.setSelected(selected);

            String acceptanceDate = bitstreamService.getMetadata(licenseBitstream, DCTERMS_RIGHTSDATE);

            if (StringUtils.isNotBlank(acceptanceDate)) {
                result.setAcceptanceDate(acceptanceDate);
                result.setGranted(true);
            } else {
                result.setGranted(false);
            }

            result.setUrl(
                configurationService.getProperty("dspace.server.url") + "/api/" + BitstreamRest.CATEGORY + "/" + English
                    .plural(BitstreamRest.NAME) + "/" + licenseBitstream.getID() + "/content");

            files.add(submissionService.buildUploadBitstream(configurationService, licenseBitstream));
        }

        if (proxyBitstream != null) {
            files.add(submissionService.buildUploadBitstream(configurationService, proxyBitstream));
        }

        result.setFiles(files);

        return result;
    }

    @Override
    public void doPatchProcessing(Context context, HttpServletRequest currentRequest, InProgressSubmission source,
            Operation op, SubmissionStepConfig stepConf) throws Exception {
        if (op.getPath().endsWith(LICENSE_STEP_SELECTED_OPERATION_ENTRY)) {
            PatchOperation<String> patchOperation = new PatchOperationFactory()
                .instanceOf(LICENSE_STEP_SELECTED_OPERATION_ENTRY, op.getOp());
            patchOperation.perform(context, currentRequest, source, op);
        } else {
            super.doPatchProcessing(context, currentRequest, source, op, stepConf);
        }
    }

    @Override
    public ErrorRest upload(Context context, SubmissionService submissionService, SubmissionStepConfig stepConfig,
                            InProgressSubmission wsi, MultipartFile file) {
        Item item = wsi.getItem();

        try {
            ProxyLicenseUtils.addProxyLicense(context, item, file);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorRest result = new ErrorRest();
            result.setMessage(e.getMessage());
            result.getPaths()
                .add("/" + WorkspaceItemRestRepository.OPERATION_PATH_SECTIONS + "/" + stepConfig.getId());
            return result;
        }

        return null;
    }

}
