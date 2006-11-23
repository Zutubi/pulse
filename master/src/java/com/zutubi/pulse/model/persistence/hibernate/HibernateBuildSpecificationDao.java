package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.util.Predicate;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class HibernateBuildSpecificationDao extends HibernateEntityDao<BuildSpecification> implements BuildSpecificationDao
{
    public Class persistentClass()
    {
        return BuildSpecification.class;
    }

    public List<BuildSpecification> findBySlave(final Slave slave)
    {
        List<BuildSpecification> all = findAll();
        List<BuildSpecification> referringToSlave = new LinkedList<BuildSpecification>();
        List<BuildSpecificationNode> nodes = new LinkedList<BuildSpecificationNode>();

        for(BuildSpecification spec: all)
        {
            spec.getRoot().getNodesByPredicate(new Predicate<BuildSpecificationNode>()
            {
                public boolean satisfied(BuildSpecificationNode node)
                {
                    BuildStage stage = node.getStage();
                    if (stage != null)
                    {
                        BuildHostRequirements hostRequirements = stage.getHostRequirements();
                        if(hostRequirements instanceof SlaveBuildHostRequirements)
                        {
                            return ((SlaveBuildHostRequirements)hostRequirements).getSlave().getId() == slave.getId();
                        }
                    }

                    return false;
                }
            }, nodes);

            if(!nodes.isEmpty())
            {
                nodes.clear();
                referringToSlave.add(spec);
            }
        }

        return referringToSlave;
    }
}
