{ pkgs, config, ... }:
{
  boot = {
    loader = {
      efi = {
        canTouchEfiVariables = true;
        efiSysMountPoint = "/boot/efi";
      };
      grub = {
        default = "saved";
        device = "nodev";
        efiSupport = true;
        useOSProber = true;
      };
    };
  };
  console.useXkbConfig = true;
  environment.systemPackages = with pkgs; [
    awesome
    curl
    fish
    powerline
    rlwrap
    tree
    vim
    unzip
    wget
    zsh
  ];
  fonts.enableDefaultPackages = true;
  hardware.pulseaudio.enable = false;
  i18n.defaultLocale = "en_US.utf8";
  imports = [
    /etc/nixos/hardware-configuration.nix
  ];
  networking.hostName = "nixos";
  networking.networkmanager.enable = true;
  nix.extraOptions = "experimental-features = nix-command flakes";
  nixpkgs.config.allowUnfree = true;
  programs = {
    command-not-found.enable = true;
    htop.enable = true;
    ssh.startAgent = true;
    sysdig.enable = false;
    systemtap.enable = false;
    thefuck.enable = true;
    tmux = {
      clock24 = true;
      enable = true;
      newSession = true;
      terminal = "screen-256color";
    };
    vim.defaultEditor = true;
    zsh = {
      autosuggestions.enable = true;
      enable = true;
      ohMyZsh = {
        enable = true;
        plugins = [
          "colored-man-pages"
          "git"
        ];
        theme = "agnoster";
      };
      syntaxHighlighting.enable = true;
    };
  };
  security.rtkit.enable = true;
  services.pipewire = {
    alsa.enable = true;
    alsa.support32Bit = true;
    enable = true;
    pulse.enable = true;
  };
  services.printing.enable = true;
  services.xserver = {
    desktopManager = {
      cinnamon.enable = true;
    };
    displayManager = {
      lightdm.enable = true;
    };
    windowManager = {
      awesome.enable = true;
    };
    xkb = {
      layout = "us";
      options = "ctrl:nocaps";
      variant = "";
    };
  };
  services.xserver.enable = true;
  sound.enable = true;
  system.stateVersion = "22.05";
  time.timeZone = "America/Chicago";
  users = {
    defaultUserShell = pkgs.zsh;
    users = {
      tnelson = {
        description = "Thomas Nelson";
        extraGroups = [
          "networkmanager"
          "wheel"
        ];
        isNormalUser = true;
        packages = with pkgs; [
          babashka
          cargo
          discord
          firefox
          gcc
          gh
          ghc
          git
          ihaskell
          python312#ipython
          leiningen
          nim
          nimble
          nodejs
          obsidian
          python3
          vim
          vscode
        ];
      };
    };
  };
}