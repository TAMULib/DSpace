/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.LicenseRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.content.Collection;
import org.dspace.content.service.CollectionService;
import org.dspace.core.Context;
import org.dspace.core.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

// TAMU Customization - proxy license step
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dspace.services.ConfigurationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
// END TAMU Customization - proxy license step

/**
 * Link repository for "license" subresource of an individual collection.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component(CollectionRest.CATEGORY + "." + CollectionRest.PLURAL_NAME + "." + CollectionRest.LICENSE)
// TAMU Customization - proxy license step
// public class CollectionLicenseLinkRepository extends AbstractDSpaceRestRepository
public class CollectionLicensesLinkRepository extends AbstractDSpaceRestRepository
// END TAMU Customization - proxy license step
    implements LinkRestRepository {

    @Autowired
    CollectionService collectionService;

    @Autowired
    LicenseService licenseService;

    // TAMU Customization - proxy license step
    @Autowired
    ConfigurationService configurationService;
    // END TAMU Customization - proxy license step

    // TAMU Customization - proxy license step - get available licenses slight refactor of getLicense
    @PreAuthorize("hasPermission(#collectionId, 'COLLECTION', 'READ')")
    public Page<LicenseRest> getLicenses(@Nullable HttpServletRequest request,
                                  UUID collectionId,
                                  @Nullable Pageable optionalPageable,
                                  Projection projection) {
        try {
            Context context = obtainContext();
            Collection collection = collectionService.find(context, collectionId);
            if (collection == null) {
                throw new ResourceNotFoundException("No such collection: " + collectionId);
            }

            List<LicenseRest> licenses = new ArrayList<>();

            Pageable pageable = utils.getPageable(optionalPageable);

            for (String filename : licenseService.getLicenseFilenames()) {
                String[] parts = filename.split("\\.");
                String license = parts[0];

                boolean isDefault = license.equalsIgnoreCase("default");

                boolean custom = false;

                String licensePath = String.join(File.separator,
                    configurationService.getProperty("dspace.dir"), "config", filename);

                String label = configurationService.getProperty(String.join(".",
                    "license", license, "label"));

                String text = isDefault
                    ? collection.getLicenseCollection()
                    : licenseService.getLicenseText(licensePath);

                if (StringUtils.isNotBlank(text)) {
                    custom = isDefault;
                } else {
                    text = licenseService.getDefaultSubmissionLicense();
                }

                licenses.add(LicenseRest.of(license, label, text, custom));
            }

            return new PageImpl(licenses, pageable, licenses.size());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // END TAMU Customization - proxy license step - get available licenses slight refactor of getLicense
}
