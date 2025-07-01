/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.core.service.LicenseService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.services.model.Request;
import org.dspace.web.ContextUtil;

// TAMU Customization - proxy license step
import java.io.FilenameFilter;
// END TAMU Customization - proxy license step

/**
 * Encapsulate the deposit license.
 *
 * @author mhwood
 */
public class LicenseServiceImpl implements LicenseService {
    private final Logger log = LogManager.getLogger();

    /**
     * The default license
     */
    protected String license;

    protected LicenseServiceImpl() {

    }

    @Override
    public void writeLicenseFile(String licenseFile,
                                 String newLicense) {
        try {
            FileOutputStream fos = new FileOutputStream(licenseFile);
            OutputStreamWriter osr = new OutputStreamWriter(fos, "UTF-8");
            PrintWriter out = new PrintWriter(osr);
            out.print(newLicense);
            out.close();
        } catch (IOException e) {
            log.warn("license_write: {}", e::getLocalizedMessage);
        }
        license = newLicense;
    }

    @Override
    public String getLicenseText(String licenseFile) {
        InputStream is = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            is = new FileInputStream(licenseFile);
            ir = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(ir);
            String lineIn;
            license = "";
            while ((lineIn = br.readLine()) != null) {
                license = license + lineIn + '\n';
            }
        } catch (IOException e) {
            log.error("Can't load configuration", e);
            throw new IllegalStateException("Failed to read default license.", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
            if (ir != null) {
                try {
                    ir.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
        return license;
    }

    /**
     * Get the site-wide default license that submitters need to grant
     *
     * Localized license requires: default_{{locale}}.license file.
     * Locale also must be listed in webui.supported.locales setting.
     *
     * @return the default license
     */
    @Override
    public String getDefaultSubmissionLicense() {
        init();
        return license;
    }

    // TAMU Customization - proxy license step - get available license filenames
    @Override
    public String[] getLicenseFilenames() {
        String homeDir = DSpaceServicesFactory.getInstance()
            .getConfigurationService()
            .getProperty("dspace.dir");
        String configDir = new StringBuilder(homeDir)
            .append(File.separator)
            .append("config")
            .toString();

        // to support localized license files see I18nUtil.getFilename

        return new File(configDir).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".license");
            }
        });
    }
    // END TAMU Customization - proxy license step - get available license filenames

    /**
     * Load in the default license.
     */
    protected void init() {
        Context context = obtainContext();
        File licenseFile = new File(I18nUtil.getDefaultLicense(context));

        FileInputStream fir = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {

            fir = new FileInputStream(licenseFile);
            ir = new InputStreamReader(fir, "UTF-8");
            br = new BufferedReader(ir);
            String lineIn;
            license = "";

            while ((lineIn = br.readLine()) != null) {
                license = license + lineIn + '\n';
            }

            br.close();

        } catch (IOException e) {
            log.error("Can't load license {}: ", licenseFile.toString(), e);

            // FIXME: Maybe something more graceful here, but with the
            // configuration we can't do anything
            throw new IllegalStateException("Cannot load license: "
                                                + licenseFile.toString(), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }

            if (ir != null) {
                try {
                    ir.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }

            if (fir != null) {
                try {
                    fir.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }

    /**
     * Obtaining current request context.
     * Return new context if getting one from current request failed.
     *
     * @return DSpace context object
     */
    private Context obtainContext() {
        try {
            Request currentRequest = DSpaceServicesFactory.getInstance().getRequestService().getCurrentRequest();
            if (currentRequest != null) {
                HttpServletRequest request = currentRequest.getHttpServletRequest();
                return ContextUtil.obtainContext(request);
            }
        } catch (Exception e) {
            log.error("Can't load current request context.");
        }

        return  new Context();
    }
}
