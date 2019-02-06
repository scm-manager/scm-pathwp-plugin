package sonia.scm.pathwp.api;

import de.otto.edison.hal.HalRepresentation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class PathWritePermissionDto extends HalRepresentation {

  private String path;
  private String name;
  private boolean group;
  private String type;

}
