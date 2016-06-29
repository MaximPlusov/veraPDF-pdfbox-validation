package org.verapdf.model.impl.pb.pd.images;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObjectProxy;
import org.verapdf.model.baselayer.Object;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class PBoxPDSMaskImage extends PBoxPDXImage implements PDSMaskImage {

	public static final String SMASK_IMAGE_TYPE = "PDSMaskImage";

	public PBoxPDSMaskImage(PDImageXObjectProxy simplePDObject, PDDocument document, PDFAFlavour flavour) {
		super(simplePDObject, SMASK_IMAGE_TYPE, document, flavour);
	}

	@Override
	public List<? extends Object> getLinkedObjects(String link) {
		switch (link) {
			case IMAGE_CS:
				return Collections.emptyList();
			default:
				return super.getLinkedObjects(link);
		}
	}
}
