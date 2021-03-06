/**
 * This file is part of veraPDF PDF Box PDF/A Validation Model Implementation, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF PDF Box PDF/A Validation Model Implementation is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF PDF Box PDF/A Validation Model Implementation as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF PDF Box PDF/A Validation Model Implementation as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.model.impl.pb.cos;

import org.verapdf.model.coslayer.CosXRef;

/**
 * Class describe special properties of cross reference table of current
 * document
 *
 * @author Evgeniy Muravitskiy
 */
public class PBCosXRef extends PBCosObject implements CosXRef {

    /** Type name for PBCosXRef */
    public static final String COS_XREF_TYPE = "CosXRef";

    private Boolean subsectionHeaderSpaceSeparated;
    private Boolean xrefEOLMarkersComplyPDFA;

    /**
     * Default constructor
     * @param subsectionHeaderSpaceSeparated is xref header spacings comply pdfa specification
     * @param xrefEOLMarkersComplyPDFA is xref eol spacings comply pdfa
     */
    public PBCosXRef(Boolean subsectionHeaderSpaceSeparated,
            Boolean xrefEOLMarkersComplyPDFA) {
        super(null, COS_XREF_TYPE);
        this.subsectionHeaderSpaceSeparated = subsectionHeaderSpaceSeparated;
        this.xrefEOLMarkersComplyPDFA = xrefEOLMarkersComplyPDFA;
    }

    /**
     * true if header of cross reference table complies PDF/A standard
     */
    @Override
    public Boolean getsubsectionHeaderSpaceSeparated() {
        return this.subsectionHeaderSpaceSeparated;
    }

    /**
     * true if EOL
     */
    @Override
    public Boolean getxrefEOLMarkersComplyPDFA() {
        return this.xrefEOLMarkersComplyPDFA;
    }
}
