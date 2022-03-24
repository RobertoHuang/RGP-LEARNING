- 创建Service Account并绑定集群角色

    ```yaml
    apiVersion: v1
    kind: ServiceAccount
    metadata:
      name: jenkins
      namespace: roberto-test
      
    ---
    apiVersion: rbac.authorization.k8s.io/v1beta1
    kind: ClusterRoleBinding
    metadata:
      name: jenkins-clusterrolebinding
      namespace: roberto-test
    roleRef:
      kind: ClusterRole
      name: cluster-admin
      apiGroup: rbac.authorization.k8s.io
    subjects:
      - kind: ServiceAccount
        name: jenkins
        namespace: roberto-test
    ```

    让`jenkins`拥有对`k8s`资源的操作权限。这一步为`jenkins`后续集成`k8s`部署打下基础