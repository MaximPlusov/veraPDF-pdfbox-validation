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
package org.verapdf.model.impl.pb.operator.specialgs;

import org.apache.pdfbox.cos.COSBase;
import org.verapdf.model.impl.pb.operator.generalgs.PBOpGeneralGS;
import org.verapdf.model.operator.Op_q_gsave;

import java.util.List;

/**
 * Operator defining the current graphics state on the
 * graphics state stack
 *
 * @author Timur Kamalov
 */
public class PBOp_q_gsave extends PBOpGeneralGS implements Op_q_gsave {

	/** Type name for {@code PBOp_q_gsave} */
    public static final String OP_Q_GSAVE_TYPE = "Op_q_gsave";

    private final int nestingLevel;

    public PBOp_q_gsave(List<COSBase> arguments, int  nestingLevel) {
        super(arguments, OP_Q_GSAVE_TYPE);
        this.nestingLevel = nestingLevel;
    }

	/**
	 * @return depth of graphics state
	 */
    @Override
    public Long getnestingLevel() {
        return Long.valueOf(this.nestingLevel);
    }

}
