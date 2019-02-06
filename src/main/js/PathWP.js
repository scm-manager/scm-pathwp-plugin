// @flow

export type PathWP = {
  path: string,
  name: string,
  group: boolean,
  type: string
};

export type PathWPs = {
  permissions: PathWP[],
  enabled: boolean
};
