package sonia.scm.pathwp.service;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "path-write-permissions")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class PathWritePermissions {

  private boolean isEnabled = false;

  @XmlElement(name = "permission")
  private List<PathWritePermission> permissions = new ArrayList<>();

}
