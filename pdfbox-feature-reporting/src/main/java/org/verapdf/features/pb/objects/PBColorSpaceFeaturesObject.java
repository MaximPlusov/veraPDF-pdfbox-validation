package org.verapdf.features.pb.objects;

import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.*;
import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.FeaturesData;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.features.IFeaturesObject;
import org.verapdf.features.pb.tools.PBCreateNodeHelper;
import org.verapdf.features.tools.ErrorsHelper;
import org.verapdf.features.tools.FeatureTreeNode;
import org.verapdf.features.tools.FeaturesCollection;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.List;

/**
 * Features object for ColorSpace
 *
 * @author Maksim Bezrukov
 */
public class PBColorSpaceFeaturesObject implements IFeaturesObject {

	private static final String ID = "id";

	private static final Logger LOGGER = Logger
			.getLogger(PBColorSpaceFeaturesObject.class);

	private PDColorSpace colorSpace;
	private String id;
	private String iccProfileChild;
	private String colorSpaceChild;

	/**
	 * Constructs new colorspace features object
	 *
	 * @param colorSpace        PDColorSpace which represents colorspace for feature report
	 * @param id                id of the object
	 * @param iccProfileChild   id of the iccprofile child
	 * @param colorSpaceChild   id of the colorspace child
	 */
	public PBColorSpaceFeaturesObject(PDColorSpace colorSpace,
									  String id,
									  String iccProfileChild,
									  String colorSpaceChild) {
		this.colorSpace = colorSpace;
		this.id = id;
		this.iccProfileChild = iccProfileChild;
		this.colorSpaceChild = colorSpaceChild;
	}

	/**
	 * @return DOCUMENT_SECURITY instance of the FeatureObjectType enumeration
	 */
	@Override
	public FeatureObjectType getType() {
		return FeatureObjectType.COLORSPACE;
	}

	/**
	 * Reports featurereport into collection
	 *
	 * @param collection collection for feature report
	 * @return FeatureTreeNode class which represents a root node of the constructed collection tree
	 * @throws FeatureParsingException occurs when wrong features tree node constructs
	 */
	@Override
	public FeatureTreeNode reportFeatures(FeaturesCollection collection) throws FeatureParsingException {
		if (colorSpace != null) {
			FeatureTreeNode root = FeatureTreeNode.createRootNode("colorSpace");

			if (id != null) {
				root.setAttribute(ID, id);
			}
			root.setAttribute("family", colorSpace.getName());

			if (colorSpace instanceof PDCIEDictionaryBasedColorSpace) {
				parseCIEDictionaryBased(root);
			} else if (colorSpace instanceof PDICCBased) {
				PDICCBased icc = (PDICCBased) colorSpace;
				if (colorSpaceChild != null) {
					FeatureTreeNode alt = root.addChild("alternate");
					alt.setAttribute(ID, colorSpaceChild);
				}
				root.addChild("components").setValue(String.valueOf(icc.getNumberOfComponents()));
				if (iccProfileChild != null) {
					FeatureTreeNode prof = root.addChild("iccProfile");
					prof.setAttribute(ID, iccProfileChild);
				}
			} else if (colorSpace instanceof PDIndexed) {
				parseIndexed(root, collection);
			} else if (colorSpace instanceof PDSeparation) {
				PDSeparation sep = (PDSeparation) colorSpace;
				if (colorSpaceChild != null) {
					FeatureTreeNode alt = root.addChild("alternate");
					alt.setAttribute(ID, colorSpaceChild);
				}
				PBCreateNodeHelper.addNotEmptyNode("colorantName", sep.getColorantName(), root);
			} else if (colorSpace instanceof PDDeviceN) {
				PDDeviceN devN = (PDDeviceN) colorSpace;
				if (colorSpaceChild != null) {
					FeatureTreeNode alt = root.addChild("alternate");
					alt.setAttribute(ID, colorSpaceChild);
				}
				List<String> devNColorantNames = devN.getColorantNames();
				if (devNColorantNames != null) {
					FeatureTreeNode colorantNames = root.addChild("colorantNames");
					for (String name : devNColorantNames) {
						PBCreateNodeHelper.addNotEmptyNode("colorantName", name, colorantNames);
					}
				}
			}

			collection.addNewFeatureTree(FeatureObjectType.COLORSPACE, root);
			return root;
		}

		return null;
	}

	/**
	 * @return null
	 */
	@Override
	public FeaturesData getData() {
		return null;
	}

	private void parseIndexed(FeatureTreeNode root, FeaturesCollection collection) throws FeatureParsingException {
		PDIndexed index = (PDIndexed) colorSpace;

		if (colorSpaceChild != null) {
			FeatureTreeNode alt = root.addChild("base");
			alt.setAttribute(ID, colorSpaceChild);
		}

		if (index.getCOSObject() instanceof COSArray) {
			FeatureTreeNode hival = root.addChild("hival");
			if (((COSArray) index.getCOSObject()).size() >= 3 &&
					((COSArray) index.getCOSObject()).getObject(2) instanceof COSNumber) {
				hival.setValue(String.valueOf(((COSNumber) ((COSArray) index.getCOSObject()).getObject(2)).intValue()));
			} else {
				ErrorsHelper.addErrorIntoCollection(collection,
						hival,
						"Indexed color space has no element hival or hival is not a number");
			}

			FeatureTreeNode lookup = root.addChild("lookup");
			if (((COSArray) index.getCOSObject()).size() >= 4) {
				byte[] lookupData = null;
				COSBase lookupTable = ((COSArray) index.getCOSObject()).getObject(3);
				if (lookupTable instanceof COSString) {
					lookupData = ((COSString) lookupTable).getBytes();
				} else if (lookupTable instanceof COSStream) {
					try {
						lookupData = (new PDStream((COSStream) lookupTable)).getByteArray();
					} catch (IOException e) {
						LOGGER.info(e);
						ErrorsHelper.addErrorIntoCollection(collection,
								lookup,
								e.getMessage());
					}
				} else {
					ErrorsHelper.addErrorIntoCollection(collection,
							lookup,
							"Indexed color space has element lookup but it is not a String or a stream");
				}

				if (lookupData != null) {
					lookup.setValue(DatatypeConverter.printHexBinary(lookupData));
				}
			} else {
				ErrorsHelper.addErrorIntoCollection(collection,
						lookup,
						"Indexed color space has no element lookup");
			}
		} else {
			ErrorsHelper.addErrorIntoCollection(collection,
					root,
					"Indexed color space is not an array");
		}
	}


	private void parseCIEDictionaryBased(FeatureTreeNode root) throws FeatureParsingException {
		PDCIEDictionaryBasedColorSpace cie = (PDCIEDictionaryBasedColorSpace) colorSpace;

		parseTristimulus(cie.getWhitepoint(), root.addChild("whitePoint"));
		parseTristimulus(cie.getBlackPoint(), root.addChild("blackPoint"));

		if (cie instanceof PDCalGray) {
			PDCalGray calGray = (PDCalGray) cie;
			root.addChild("gamma").setValue(String.valueOf(calGray.getGamma()));
		} else if (cie instanceof PDCalRGB) {
			PDCalRGB calRGB = (PDCalRGB) cie;
			FeatureTreeNode gamma = root.addChild("gamma");
			PDGamma pdGamma = calRGB.getGamma();
			gamma.setAttribute("red", String.valueOf(pdGamma.getR()));
			gamma.setAttribute("green", String.valueOf(pdGamma.getG()));
			gamma.setAttribute("blue", String.valueOf(pdGamma.getB()));
			parseFloatArray(calRGB.getMatrix(), root.addChild("matrix"));
		} else if (cie instanceof PDLab) {
			PDLab lab = (PDLab) cie;
			FeatureTreeNode range = root.addChild("range");
			range.setAttribute("aMin", String.valueOf(lab.getARange().getMin()));
			range.setAttribute("aMax", String.valueOf(lab.getARange().getMax()));
			range.setAttribute("bMin", String.valueOf(lab.getBRange().getMin()));
			range.setAttribute("bMax", String.valueOf(lab.getBRange().getMax()));
		}

	}

	private static void parseFloatArray(float[] array, FeatureTreeNode parent) throws FeatureParsingException {
		for (int i = 0; i < array.length; ++i) {
			FeatureTreeNode element = parent.addChild("element");
			element.setAttribute("number", String.valueOf(i));
			element.setAttribute("value", String.valueOf(array[i]));
		}
	}

	private static void parseTristimulus(PDTristimulus tris, FeatureTreeNode curNode) {
		curNode.setAttribute("x", String.valueOf(tris.getX()));
		curNode.setAttribute("y", String.valueOf(tris.getY()));
		curNode.setAttribute("z", String.valueOf(tris.getZ()));
	}
}
