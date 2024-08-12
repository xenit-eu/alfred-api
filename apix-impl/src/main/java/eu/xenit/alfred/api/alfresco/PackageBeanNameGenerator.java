package eu.xenit.alfred.api.alfresco;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

/**
 * Used to specify the full name + package for autowired (@Component) beans Currently only for the amp version of apix,
 * not for the DE version Created by Michiel Huygen on 19/04/2016.
 */
public class PackageBeanNameGenerator implements BeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry beanDefinitionRegistry) {
        return beanDefinition.getBeanClassName();
    }
}
