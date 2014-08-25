package ext.deployit.community.plugin.manualstep.util;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugin.overthere.HostContainer;

public final class Util {

	public final static String getContainerHostName(final ConfigurationItem containerItem)	{
    	return (containerItem instanceof Host) ? 
    			((Host) containerItem).getName() : 
    				((containerItem instanceof HostContainer) ? 
    						((HostContainer) containerItem).getName() : 
    							null);
	}
	
}
