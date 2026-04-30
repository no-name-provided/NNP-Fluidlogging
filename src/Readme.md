<H1>General Information</H1>
This mod adds very general fluidlogging to Minecraft+NeoForge. As it is mixin based, it can be adapted for other
mod loaders with relative ease. I will not be doing this. As the vanilla fluid system is seldom meaningfully updated,
it should be easy to port this forward. I intend to do that, once this mod reaches beta status and I've addressed major
bugs. I will not backport, as older versions have alternatives that are easily available, and claim to support general
fluidlogging.

I don't intend to push this mod past late beta or early release. I am providing it because I think it's unfortunate no
one else has bothered and it really isn't that hard to whip up a basic implementation. It is my hope other, more
interested, folks will contribute to this foundation and build this up into a great mod. If not, oh well, this will
still be pretty decent.

Anyway, feel free to request features, and please do report bugs. However, if you aren't afraid to try
your hand at coding, consider taking a crack at fixing some of those bugs, or adding whatever feature
you want to see. PRs appreciated; just make sure you agree to the license.

<H1>How It Works</H1>
Expanding Minecraft's fluidlogging capabilities can be a bit tedious, but it's mostly quick and easy.
This mod does three things:
<ol>
<li>Create a data structure to hold a list of BlockPos with (nonwater) fluid states. 
This is updated anytime a block <i>position's</i> fluid state is changed. 
</li>
<ol>
<li> We currently use NeoForge chunk attachments for this, so we don't need to worry about syncing and we 
don't need to parse data for unloaded chunks. However, you could easily switch to a save
data approach and port this feature to Fabric.
</li>
</ol>
<li>Attach a copy of this data structure to each chunk with special fluid states</li>
<li>Replace every (relevant) call to BlockState#getFluidState() with a position dependent
check (typically BlockAndTintGetter#getFluidState). 
<ol>
<li>This is most of the work, and almost exclusively 
done with mixins. This is where the tedium comes in. This mod is mostly mixins that replace
#getFluidState with #getFluidState(pos). If you find a bug, it's likely I skipped (or missed)
one of these mixins.
</li>
</ol>
</ol>
That's basically all you need to do. We have a few mixins to SimpleWaterloggedBlock, FlowingFluid, etc. 
to jailbreak hard-coded checks and update our data structure. We also special case vanilla water, 
since it behaves a bit differently, and we currently bypass a lot of caching that we should probably 
reimplement, but that's it.
