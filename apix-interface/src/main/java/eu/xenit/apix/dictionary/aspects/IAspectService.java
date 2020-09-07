package eu.xenit.apix.dictionary.aspects;

import eu.xenit.apix.data.QName;

public interface IAspectService {

    AspectDefinition GetAspectDefinition(QName qname);

    Aspects getAspects();
}
