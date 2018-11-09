package bot;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CommonBase {

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
