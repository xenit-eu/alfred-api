package eu.xenit.alfred.api.dictionary.aspects;

import eu.xenit.alfred.api.data.QName;

public interface IAspectService {

    AspectDefinition GetAspectDefinition(QName qname);

    Aspects getAspects();
}
